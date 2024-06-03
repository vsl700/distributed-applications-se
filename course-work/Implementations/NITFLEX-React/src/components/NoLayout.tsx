import './NoLayout.css'

export const NoLayout = (props: {children: React.ReactElement | React.ReactElement[]}) => {
    return (
        <div className="NoLayout-container">
            {props.children}
        </div>
    )
}