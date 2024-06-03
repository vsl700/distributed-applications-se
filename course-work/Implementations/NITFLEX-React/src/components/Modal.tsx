import cross from '../assets/cross.svg'
import './Modal.css'

export const Modal = (props: {isOpen?: boolean, onClose: Function, children: React.ReactElement | React.ReactElement[]}) => {
    return (
        <div className={`Modal${props.isOpen ? '' : ' Modal-hidden'}`}>
            <div className='Modal-content'>
                <img className='Modal-close nitflex-button' src={cross} alt='Close modal' onClick={() => props.onClose()} />
                {props.isOpen && props.children}
            </div>
        </div>
    )
}